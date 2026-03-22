let accessToken = null;

export const setAuthToken = (token) => {
  accessToken = token;
};

export const getAuthToken = () => accessToken;
