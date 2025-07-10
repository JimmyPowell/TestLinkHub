import dayjs from 'dayjs';

/**
 * Formats a date-time string into a more readable format.
 * @param {string} dateTime - The ISO date-time string.
 * @param {string} [format='YYYY-MM-DD HH:mm:ss'] - The target format.
 * @returns {string} - The formatted date-time string, or 'N/A' if input is invalid.
 */
export function formatDateTime(dateTime, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!dateTime) {
    return 'N/A';
  }
  const date = dayjs(dateTime);
  return date.isValid() ? date.format(format) : 'Invalid Date';
}
