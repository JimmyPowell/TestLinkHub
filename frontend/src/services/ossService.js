import OSS from 'ali-oss';
import userService from './userService';

const ossService = {
  async uploadFile(file) {
    try {
      // 1. 获取STS凭证
      const response = await userService.getSTSCredentials();
      console.log('STS Response:', response);
      const creds = response.data.data;
      console.log('STS Credentials:', creds);

      // 2. 初始化OSS客户端
      const client = new OSS({
        region: creds.region,
        accessKeyId: creds.access_key_id,
        accessKeySecret: creds.access_key_secret,
        stsToken: creds.security_token,
        bucket: creds.bucket_name
      });

      // 3. 生成文件名
      const fileName = `${Date.now()}-${file.name}`;

      // 4. 上传文件
      const result = await client.put(fileName, file);

      // 5. 返回文件URL
      return result.url;
    } catch (error) {
      console.error('OSS upload failed:', error);
      throw error;
    }
  }
};

export default ossService;
