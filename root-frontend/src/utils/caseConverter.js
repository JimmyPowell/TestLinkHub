function toSnake(str) {
  return str.replace(/([A-Z])/g, "_$1").toLowerCase();
}

function toCamel(str) {
  return str.replace(/([_][a-z])/g, (group) => group.toUpperCase().replace('_', ''));
}

export function convertToCamelCase(obj) {
  if (obj === null || typeof obj !== 'object') {
    return obj;
  }

  if (Array.isArray(obj)) {
    return obj.map(v => convertToCamelCase(v));
  }

  return Object.keys(obj).reduce((acc, key) => {
    const camelKey = toCamel(key);
    acc[camelKey] = convertToCamelCase(obj[key]);
    return acc;
  }, {});
}

export function convertToSnakeCase(obj) {
  if (obj === null || typeof obj !== 'object') {
    return obj;
  }

  if (Array.isArray(obj)) {
    return obj.map(v => convertToSnakeCase(v));
  }

  return Object.keys(obj).reduce((acc, key) => {
    const snakeKey = toSnake(key);
    acc[snakeKey] = convertToSnakeCase(obj[key]);
    return acc;
  }, {});
}
