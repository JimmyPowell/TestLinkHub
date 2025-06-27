import apiClient from './api';

// Helper function to convert camelCase keys to snake_case
const toSnakeCase = (obj) => {
  if (obj === null || typeof obj !== 'object') {
    return obj;
  }
  if (Array.isArray(obj)) {
    return obj.map(v => toSnakeCase(v));
  }
  return Object.keys(obj).reduce((acc, key) => {
    const snakeKey = key.replace(/[A-Z]/g, letter => `_${letter.toLowerCase()}`);
    acc[snakeKey] = toSnakeCase(obj[key]);
    return acc;
  }, {});
};

export default {
  /**
   * Fetches users for an admin.
   * @param {object} params - The query parameters.
   * @param {number} params.page - The page number.
   * @param {number} params.size - The page size.
   * @returns {Promise} - The promise from the API call.
   */
  getAdminUsers(params) {
    return apiClient.get('/admin/users', { params });
  },

  /**
   * Fetches users for a company with optional search filters.
   * @param {object} params - The query parameters.
   * @param {number} params.page - The page number.
   * @param {number} params.size - The page size.
   * @param {string} [params.uuid] - The UUID of the user to search for.
   * @param {string} [params.username] - The username to search for.
   * @param {string} [params.status] - The status of the user to search for.
   * @param {string} [params.phoneNumber] - The phone number of the user to search for.
   * @returns {Promise} - The promise from the API call.
   */
  getCompanyUsers(params) {
    // Filter out null or empty string values from params
    const filteredParams = Object.entries(params).reduce((acc, [key, value]) => {
      if (value !== null && value !== '') {
        acc[key] = value;
      }
      return acc;
    }, {});
    return apiClient.get('/admin/company/users', { params: filteredParams });
  },

  /**
   * Fetches the detailed information for a single user.
   * @param {string} uuid - The UUID of the user.
   * @returns {Promise} - The promise from the API call.
   */
  getUserDetails(uuid) {
    return apiClient.get(`/admin/company/users/${uuid}/details`);
  },

  /**
   * Creates a new user for the company.
   * @param {object} userData - The data for the new user in camelCase.
   * @returns {Promise} - The promise from the API call.
   */
  createCompanyUser(userData) {
    const snakeCaseData = toSnakeCase(userData);
    return apiClient.post('/admin/company/users', snakeCaseData);
  },

  /**
   * Updates an existing user for the company.
   * @param {string} uuid - The UUID of the user to update.
   * @param {object} userData - The updated data for the user in camelCase.
   * @returns {Promise} - The promise from the API call.
   */
  updateCompanyUser(uuid, userData) {
    const snakeCaseData = toSnakeCase(userData);
    return apiClient.put(`/admin/company/users/${uuid}`, snakeCaseData);
  },

  /**
   * Removes a user from the company.
   * @param {string} uuid - The UUID of the user to remove.
   * @returns {Promise} - The promise from the API call.
   */
  removeUserFromCompany(uuid) {
    return apiClient.post(`/admin/company/users/${uuid}/remove`);
  },

  /**
   * Fetches the current logged-in user's info (name and role).
   * @returns {Promise} - The promise from the API call.
   */
  getMe() {
    return apiClient.get('/client/user/me');
  },

  /**
   * Fetches STS credentials for OSS.
   * @returns {Promise} - The promise from the API call.
   */
  getSTSCredentials() {
    return apiClient.get('/oss/sts');
  },
};
