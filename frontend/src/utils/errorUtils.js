export function getErrorMessage(error, fallback = 'Something went wrong. Please try again.') {
  if (!error) return fallback;

  const responseData = error.response?.data;
  if (typeof responseData === 'string') {
    return responseData;
  }

  if (responseData?.message) {
    return responseData.message;
  }

  if (responseData?.error) {
    return responseData.error;
  }

  if (error.message) {
    return error.message;
  }

  return fallback;
}
