import React, { useState } from "react";
import Image from "../assets/image.png";
import Logo from "../assets/logo.png";
import { FaEye, FaEyeSlash } from "react-icons/fa6";
import { Link } from "react-router-dom";
import api from "../api/axios";
import { useNavigate } from "react-router-dom";

const Signup = () => {
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [formData, setFormData] = useState({
    username: "",
    userEmail: "",
    userPassword: "",
  });

  const handleSignUp = async () => {
    try {
      console.log(formData);
      const response = await api.post("/register", formData);
      if (response.status == 201) {
        navigate("/login");
      }
      console.log(response.data);
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <div className="login-main">
      <div className="login-left">
        <img src={Image} alt="" />
      </div>

      <div className="login-right">
        <div className="login-right-container">
          <div className="login-logo">
            <img src={Logo} alt="" />
          </div>

          <div className="login-center">
            <h2>Create an account</h2>
            <p>Please enter your details</p>

            <form>
              <input
                type="text"
                placeholder="Full Name"
                name="FullName"
                onChange={(e) => {
                  setFormData({
                    ...formData,
                    username: e.target.value,
                  });
                }}
              />
              <input
                type="email"
                placeholder="Email"
                name="Email"
                onChange={(e) => {
                  setFormData({
                    ...formData,
                    userEmail: e.target.value,
                  });
                }}
              />

              <div className="pass-input-div">
                <input
                  type={showPassword ? "text" : "password"}
                  placeholder="Password"
                  name="Password"
                  onChange={(e) => {
                    setFormData({
                      ...formData,
                      userPassword: e.target.value,
                    });
                  }}
                />
                {showPassword ? (
                  <FaEyeSlash onClick={() => setShowPassword(!showPassword)} />
                ) : (
                  <FaEye onClick={() => setShowPassword(!showPassword)} />
                )}
              </div>

              <div className="pass-input-div">
                <input
                  type={showConfirmPassword ? "text" : "password"}
                  placeholder="Confirm Password"
                />
                {showConfirmPassword ? (
                  <FaEyeSlash
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  />
                ) : (
                  <FaEye
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  />
                )}
              </div>

              <div className="login-center-buttons">
                <button type="button" onClick={handleSignUp}>
                  Sign Up
                </button>
              </div>
            </form>
          </div>

          <p className="login-bottom-p">
            Already have an account? <Link to="/login">Log In</Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Signup;
