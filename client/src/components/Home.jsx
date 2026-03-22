import React, { useState, useMemo, useRef, useEffect } from "react";
import styles from "./Dashboard.module.css";
import api from "../api/axios";
import { setAuthToken } from "../api/authToken";
import { useAuth } from "../context/AuthContext";

const FILES_PER_PAGE = 6;

export default function Dashboard() {
  const { setAccessToken } = useAuth();

  const [files, setFiles] = useState([]);
  const [selectedFile, setSelectedFile] = useState(null);
  const [error, setError] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const fileInputRef = useRef(null);

  /* ───────────────────────────── */
  /* Fetch Files                   */
  /* GET /files                    */
  /* ───────────────────────────── */
  const handleGetFiles = async () => {
    try {
      const res = await api.get("/files");

      const normalizedFiles = (res.data || []).map((file) => ({
        id: file.id,
        name: file.originalName,
        type: file.contentType?.split("/")[1]?.toUpperCase() || "FILE",
        size: formatBytes(file.size),
        uploadDate: file.createdAt?.split("T")[0] || "",
        storage: file.storage,
      }));

      setFiles(normalizedFiles);
    } catch (err) {
      console.error(err);
      setFiles([]);
    }
  };

  useEffect(() => {
    handleGetFiles();
  }, []);

  /* ───────────────────────────── */
  /* File Selection                */
  /* ───────────────────────────── */
  const handleFileChange = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setSelectedFile(file);
  };

  /* ───────────────────────────── */
  /* Normal Upload (<5MB)          */
  /* POST /files → PUT presigned   */
  /* PUT /files/:fileId (confirm)  */
  /* ───────────────────────────── */
  const normalFileUpload = async () => {
    try {
      // 1. Request presigned URL — backend expects fileName + contentType
      const response = await api.post("/files", {
        fileName: selectedFile.name,
        contentType: selectedFile.type,
      });

      const { presignedUrl, fileId } = response.data;

      // 2. PUT file directly to S3
      const uploadRes = await fetch(uploadURL, {
        method: "PUT",
        headers: { "Content-Type": selectedFile.type },
        body: selectedFile,
      });

      if (!uploadRes.ok) throw new Error("File upload to S3 failed");

      // 3. Confirm upload with backend
      await api.put(`/files/${fileId}`);

      alert("File uploaded successfully!");
      resetAfterUpload();
    } catch (error) {
      console.error(error);
      alert("Upload failed");
    }
  };

  /* ───────────────────────────── */
  /* Multipart Upload (>=5MB)      */
  /* POST /files/multipart         */
  /* GET  /files/multipart/:id     */
  /* PUT each part via presigned   */
  /* POST /files/multipart/api     */
  /* POST /files/multipart/        */
  /*      complete/:fileId         */
  /* ───────────────────────────── */
  const multipartUpload = async () => {
    let fileId = null;

    try {
      const chunkSize = 5 * 1024 * 1024;
      const totalParts = Math.ceil(selectedFile.size / chunkSize);

      // 1. Start multipart — backend needs fileName, contentType, totalParts
      const metaRes = await api.post("/files/multipart", {
        fileName: selectedFile.name,
        contentType: selectedFile.type,
        totalParts: String(totalParts),
      });

     
      const presignedUrls = metaRes.data.presignedUrls;
   
     
      for (let i = 1; i <= presignedUrls.length; i++) {
        const url = presignedUrls[i];
        const start = i * chunkSize;
        const chunk = selectedFile.slice(start, start + chunkSize);

        const uploadRes = await fetch(url, {
          method: "PUT",
          body: chunk,
        });

        if (!uploadRes.ok) {
          throw new Error(`Failed to upload part ${i}`);
        }

        const etag = (
          uploadRes.headers.get("ETag") ||
          uploadRes.headers.get("etag") ||
          ""
        ).replace(/"/g, "");

        if (!etag) throw new Error(`Missing ETag for part ${i}`);

        // 4. Confirm each part with the backend
        await api.post("/files/multipart/api", {
          fileId: String(fileId),
          partNumber: String(part.partNumber),
          eTag: etag,
        });
      }

      // 5. Complete the multipart upload
      await api.post(`/files/multipart/complete/${fileId}`);

      alert("File uploaded successfully!");
      resetAfterUpload();
    } catch (error) {
      console.error("Multipart error:", error);

      // Abort on failure
      if (fileId) {
        try {
          await api.delete(`/files/multipart/${fileId}`);
          console.log("Multipart upload aborted successfully");
        } catch (abortError) {
          console.error("Failed to abort upload:", abortError);
        }
      }

      alert("Upload failed and was cancelled.");
    }
  };

  /* ───────────────────────────── */
  /* Upload Handler                */
  /* ───────────────────────────── */
  const handleFileUpload = async () => {
    if (!selectedFile) {
      alert("Please select a file first");
      return;
    }

    const fileLimit = 5 * 1024 * 1024;

    if (selectedFile.size < fileLimit) {
      return normalFileUpload();
    } else {
      return multipartUpload();
    }
  };

  /* ───────────────────────────── */
  /* Reset UI After Upload         */
  /* ───────────────────────────── */
  const resetAfterUpload = () => {
    setSelectedFile(null);
    if (fileInputRef.current) fileInputRef.current.value = "";
    handleGetFiles();
  };

  /* ───────────────────────────── */
  /* Search + Pagination           */
  /* ───────────────────────────── */
  const filteredFiles = useMemo(() => {
    if (!searchQuery.trim()) return files;
    const q = searchQuery.toLowerCase();
    return files.filter((f) => f.name.toLowerCase().includes(q));
  }, [files, searchQuery]);

  const totalPages = Math.max(1, Math.ceil(filteredFiles.length / FILES_PER_PAGE));
  const safePage = Math.min(currentPage, totalPages);
  const paginatedFiles = filteredFiles.slice(
    (safePage - 1) * FILES_PER_PAGE,
    safePage * FILES_PER_PAGE
  );

  /* ───────────────────────────── */
  /* Download                      */
  /* GET /files/:id                */
  /* ───────────────────────────── */
  const handleDownload = async (file) => {
    try {
      const resp = await api.get(`/files/${file.id}`);
      // Backend returns a File entity; use the presignedUrl or downloadUrl field
      const url = resp.data.presignedUrl || resp.data.downloadUrl || resp.data.fileUrl;
      if (!url) throw new Error("No download URL returned");
      window.open(url, "_blank");
    } catch (err) {
      setError(err.message);
    }
  };

  /* ───────────────────────────── */
  /* Delete                        */
  /* DELETE /files/:id             */
  /* ───────────────────────────── */
  const handleDelete = async (id) => {
    try {
      await api.delete(`/files/${id}`);
      setFiles((prev) => prev.filter((f) => f.id !== id));
    } catch (err) {
      setError(err.message);
    }
  };

  /* ───────────────────────────── */
  /* Logout                        */
  /* ───────────────────────────── */
  const handleLogout = async () => {
    try {
      await api.post("/auth/logout");
      setAccessToken(null);
      setAuthToken(null);
      window.location.href = "/login";
    } catch (err) {
      console.error("Logout failed:", err);
    }
  };

  /* ───────────────────────────── */
  /* UI                            */
  /* ───────────────────────────── */
  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <h1 className={styles.title}>Your Files</h1>
        <button className={styles.logoutBtn} onClick={handleLogout}>
          Logout
        </button>
      </header>

      <section className={styles.uploadSection}>
        <input
          ref={fileInputRef}
          type="file"
          className={styles.fileInput}
          onChange={handleFileChange}
        />
        <button className={styles.uploadBtn} onClick={handleFileUpload}>
          Upload
        </button>
      </section>

      <div className={styles.searchWrapper}>
        <input
          type="text"
          className={styles.searchInput}
          placeholder="Search files by name…"
          value={searchQuery}
          onChange={(e) => {
            setSearchQuery(e.target.value);
            setCurrentPage(1);
          }}
        />
      </div>

      {paginatedFiles.length > 0 ? (
        <div className={styles.fileGrid}>
          {paginatedFiles.map((file) => (
            <div key={file.id} className={styles.fileCard}>
              <p className={styles.fileName}>{file.name}</p>
              <p className={styles.fileMeta}>
                {file.type} · {file.size}
                <br />
                Uploaded {file.uploadDate}
              </p>
              <div className={styles.fileActions}>
                <button
                  className={styles.downloadBtn}
                  onClick={() => handleDownload(file)}
                >
                  Download
                </button>
                <button
                  className={styles.deleteBtn}
                  onClick={() => handleDelete(file.id)}
                >
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className={styles.emptyState}>
          {searchQuery ? "No files match your search." : "No files uploaded yet."}
        </div>
      )}

      {filteredFiles.length > FILES_PER_PAGE && (
        <nav className={styles.pagination}>
          <button
            className={styles.pageBtn}
            onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
            disabled={safePage <= 1}
          >
            Previous
          </button>

          {Array.from({ length: totalPages }, (_, i) => i + 1).map((page) => (
            <button
              key={page}
              className={`${styles.pageBtn} ${
                page === safePage ? styles.pageBtnActive : ""
              }`}
              onClick={() => setCurrentPage(page)}
            >
              {page}
            </button>
          ))}

          <button
            className={styles.pageBtn}
            onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
            disabled={safePage >= totalPages}
          >
            Next
          </button>
        </nav>
      )}
    </div>
  );
}

/* ───────────────────────────── */
function formatBytes(bytes) {
  if (bytes === 0) return "0 B";
  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + " " + sizes[i];
}