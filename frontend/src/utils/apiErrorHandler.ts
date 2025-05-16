/**
 * Utility functions for handling API errors
 */

/**
 * Maps server validation errors to React-Admin field errors format
 * @param errorResponse The error response from the server
 * @returns An object with field errors for React-Admin form
 */
export const mapValidationErrors = (errorResponse: any) => {
  // If there's no response or no body, return an empty object
  if (!errorResponse || !errorResponse.body) {
    return {};
  }

  // If there are field-specific errors
  if (errorResponse.status === 400 && errorResponse.body.errors) {
    return errorResponse.body.errors;
  }

  // If there's a general error message
  if (errorResponse.body.message) {
    return { _error: errorResponse.body.message };
  }

  return {};
};

/**
 * Checks if an API error is a validation error
 * @param error The error object
 * @returns True if it's a validation error, false otherwise
 */
export const isValidationError = (error: any) => {
  return error && error.status === 400 && error.body && (error.body.errors || error.body.message);
};
