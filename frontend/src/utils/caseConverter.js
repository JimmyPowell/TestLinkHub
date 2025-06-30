// 辅助函数：将对象键从驼峰转换为下划线
export const convertToSnakeCase = (obj) => {
  if (obj === null || typeof obj !== 'object') {
    return obj;
  }

  if (Array.isArray(obj)) {
    return obj.map(v => convertToSnakeCase(v));
  }

  return Object.keys(obj).reduce((acc, key) => {
    const snakeKey = key.replace(/[A-Z]/g, letter => `_${letter.toLowerCase()}`);
    acc[snakeKey] = convertToSnakeCase(obj[key]);
    return acc;
  }, {});
};
