
function rethrow(err) {
  if (err.response) {
    const data = err.response.data;
    let message = err.message;

    if (data && typeof data === "object") {
      const fieldErrors = Array.isArray(data.errors)
        ? data.errors.map((e) => e.defaultMessage).filter(Boolean)
        : [];

      message =
        (fieldErrors.length && fieldErrors.join("; ")) ||
        data.detail ||
        data.message ||
        message;
    } else if (typeof data === "string" && data) {
      message = data;
    }

    const error = new Error(message);
    error.status = err.response.status;
    throw error;
  }
  throw err;
}

module.exports = { rethrow };
