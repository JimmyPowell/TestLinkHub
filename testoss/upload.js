// OSS配置信息（从t1.js获取STS凭证）
const ossConfig = {
    endpoint: "oss-cn-beijing.aliyuncs.com",
    bucketName: "cosv-horizon",
    // 使用STS临时凭证
    accessKeyId: "STS.NVWmbhcnMhEhnKsqHjHLorR9P",
    accessKeySecret: "GPhLfHxXEAQGartoG3hRCSZddwqGVNutnZSik825WWiD",
    stsToken: "CAISuQJ1q6Ft5B2yfSjIr5XiJtjcjrFs34eDbG3ClUg/RMNDnZeSsjz2IHhMfHFoAeoas/81lGlR7vwZlqJIRoReREvCUcZr8syLUqA94NCT1fau5Jko1beHewHKeTOZsebWZ+LmNqC/Ht6md1HDkAJq3LL+bk/Mdle5MJqP+/UFB5ZtKWveVzddA8pMLQZPsdITMWCrVcygKRn3mGHdfiEK00he8Togufzjk5HCs0CF0w2jlb8vyt6vcsT+Xa5FJ4xiVtq55utye5fa3TRYgxowr/8u1PQcoW2b44rHUwAIu0/dKYHT6cZ/hoOFqVu558WuxdmLdp0Q0oS7/+yfQKbXu3Sa7Ar1cCGxEuoF6dJYJ1cDD8LfEV7jvJ6BeOf7nU1qnzyVSo1k1XRDTl00PXgGLld+QpQr8syXCm/LGoABgUQmFPQqC6GqInjDU8QwqcEP+4e+i7ZWDAtZaz8IFu7Ft/xU7QOYQ4jlkbqBQVQx1CUubje5PgsU2ackkP/84C/wujUuqiK2+qYrMpQ/b2H3pVFy0+q0Gbymj+U59wo6S0DF9dIdJTyoOcblODoYBKuQzj53PfVemX9rfWzg1y0gAA=="
};

// 存储待上传的文件
let fileList = [];

// DOM 元素
const dropZone = document.getElementById('dropZone');
const fileInput = document.getElementById('fileInput');
const previewContainer = document.getElementById('previewContainer');
const uploadBtn = document.getElementById('uploadBtn');
const clearBtn = document.getElementById('clearBtn');
const resultList = document.getElementById('resultList');

// 初始化OSS客户端
function initOSSClient() {
    return new OSS({
        region: ossConfig.endpoint.split('.')[0],
        accessKeyId: ossConfig.accessKeyId,
        accessKeySecret: ossConfig.accessKeySecret,
        stsToken: ossConfig.stsToken,
        bucket: ossConfig.bucketName,
    });
}

// 文件选择处理函数
fileInput.addEventListener('change', handleFileSelect);
dropZone.addEventListener('click', () => fileInput.click());
dropZone.addEventListener('dragover', handleDragOver);
dropZone.addEventListener('dragleave', handleDragLeave);
dropZone.addEventListener('drop', handleDrop);
uploadBtn.addEventListener('click', uploadFiles);
clearBtn.addEventListener('click', clearFiles);

// 文件拖拽处理
function handleDragOver(e) {
    e.preventDefault();
    e.stopPropagation();
    dropZone.classList.add('active');
}

// 文件拖拽离开处理
function handleDragLeave(e) {
    e.preventDefault();
    e.stopPropagation();
    dropZone.classList.remove('active');
}

// 文件放置处理
function handleDrop(e) {
    e.preventDefault();
    e.stopPropagation();
    dropZone.classList.remove('active');
    
    const dt = e.dataTransfer;
    const files = dt.files;
    
    handleFiles(files);
}

// 文件选择处理
function handleFileSelect(e) {
    const files = e.target.files;
    handleFiles(files);
}

// 处理选中的文件
function handleFiles(files) {
    for (let i = 0; i < files.length; i++) {
        const file = files[i];
        
        // 只处理图片文件
        if (!file.type.match('image.*')) {
            alert(`${file.name} 不是图片文件，已跳过`);
            continue;
        }
        
        // 检查是否已存在相同文件
        const exists = fileList.some(f => f.name === file.name && f.size === file.size);
        if (exists) {
            continue;
        }
        
        fileList.push(file);
        
        // 创建预览元素
        const previewItem = document.createElement('div');
        previewItem.className = 'preview-item';
        
        const img = document.createElement('img');
        const reader = new FileReader();
        
        reader.onload = (function(aImg) { 
            return function(e) { 
                aImg.src = e.target.result; 
            }; 
        })(img);
        
        reader.readAsDataURL(file);
        
        const fileInfo = document.createElement('div');
        fileInfo.className = 'file-info';
        fileInfo.textContent = file.name;
        
        const removeBtn = document.createElement('button');
        removeBtn.className = 'remove-btn';
        removeBtn.textContent = '×';
        removeBtn.addEventListener('click', function() {
            const index = fileList.findIndex(f => f.name === file.name);
            if (index !== -1) {
                fileList.splice(index, 1);
                previewItem.remove();
                
                if (fileList.length === 0) {
                    previewContainer.style.display = 'none';
                }
            }
            updateUploadButtonState();
        });
        
        previewItem.appendChild(img);
        previewItem.appendChild(fileInfo);
        previewItem.appendChild(removeBtn);
        
        previewContainer.appendChild(previewItem);
        previewContainer.style.display = 'flex';
    }
    
    // 更新上传按钮状态
    updateUploadButtonState();
    
    // 重置文件输入框，允许重复选择相同文件
    fileInput.value = '';
}

// 更新上传按钮状态
function updateUploadButtonState() {
    uploadBtn.disabled = fileList.length === 0;
}

// 清除所有文件
function clearFiles() {
    fileList = [];
    previewContainer.innerHTML = '';
    previewContainer.style.display = 'none';
    updateUploadButtonState();
    resultList.innerHTML = '';
}

// 上传文件
async function uploadFiles() {
    if (fileList.length === 0) return;
    
    // 禁用上传按钮，避免重复操作
    uploadBtn.disabled = true;
    uploadBtn.textContent = '上传中...';
    
    try {
        const client = initOSSClient();
        
        // 上传每个文件
        for (let i = 0; i < fileList.length; i++) {
            const file = fileList[i];
            const fileName = `uploads/${Date.now()}_${file.name}`;
            
            try {
                // 添加上传条目，准备显示进度
                addResultItem(file.name, '', true);
                
                // 设置进度回调
                const options = {
                    progress: function(p) {
                        updateProgress(file.name, Math.floor(p * 100));
                    },
                    headers: {
                        // 设置该Object的访问权限为公共读
                        'x-oss-object-acl': 'public-read'
                    }
                };
                
                // 执行上传
                const result = await client.multipartUpload(fileName, file, options);
                
                // 获取可访问的URL
                const url = client.signatureUrl(fileName);
                
                // 更新上传结果
                updateResultSuccess(file.name, url);
            } catch (error) {
                console.error(`上传 ${file.name} 失败:`, error);
                updateResultError(file.name, error.message || '上传失败');
            }
        }
    } catch (error) {
        console.error('创建OSS客户端出错:', error);
        alert(`初始化上传失败: ${error.message}`);
    } finally {
        // 恢复上传按钮状态
        uploadBtn.disabled = false;
        uploadBtn.textContent = '开始上传';
        
        // 清空已上传的文件列表和预览
        fileList = [];
        previewContainer.innerHTML = '';
        previewContainer.style.display = 'none';
        updateUploadButtonState();
    }
}

// 添加结果项
function addResultItem(fileName, url, isUploading = true) {
    const resultItem = document.createElement('div');
    resultItem.className = 'result-item';
    resultItem.setAttribute('data-name', fileName);
    
    const fileNameEl = document.createElement('div');
    fileNameEl.className = 'result-name';
    fileNameEl.textContent = fileName;
    
    const progressContainer = document.createElement('div');
    progressContainer.className = 'progress-container';
    
    const progressBar = document.createElement('div');
    progressBar.className = 'progress-bar';
    progressBar.style.width = '0%';
    progressBar.textContent = '0%';
    
    progressContainer.appendChild(progressBar);
    
    const resultInfo = document.createElement('div');
    resultInfo.className = 'result-info';
    
    if (isUploading) {
        resultInfo.textContent = '准备上传...';
    } else {
        const linkContainer = document.createElement('div');
        linkContainer.className = 'link-container';
        
        const linkEl = document.createElement('a');
        linkEl.href = url;
        linkEl.target = '_blank';
        linkEl.textContent = url;
        
        const copyBtn = document.createElement('button');
        copyBtn.className = 'copy-btn';
        copyBtn.textContent = '复制';
        copyBtn.addEventListener('click', function() {
            navigator.clipboard.writeText(url).then(() => {
                copyBtn.textContent = '已复制';
                setTimeout(() => {
                    copyBtn.textContent = '复制';
                }, 2000);
            });
        });
        
        linkContainer.appendChild(linkEl);
        linkContainer.appendChild(copyBtn);
        
        resultInfo.appendChild(linkContainer);
    }
    
    resultItem.appendChild(fileNameEl);
    resultItem.appendChild(progressContainer);
    resultItem.appendChild(resultInfo);
    
    resultList.appendChild(resultItem);
    
    return resultItem;
}

// 更新上传进度
function updateProgress(fileName, progress) {
    const progressBar = document.querySelector(`.result-item[data-name="${fileName}"] .progress-bar`);
    if (progressBar) {
        progressBar.style.width = `${progress}%`;
        progressBar.textContent = `${progress}%`;
    }
}

// 更新上传成功结果
function updateResultSuccess(fileName, url) {
    const resultItem = document.querySelector(`.result-item[data-name="${fileName}"]`);
    if (resultItem) {
        resultItem.className = 'result-item success';
        
        const resultInfo = resultItem.querySelector('.result-info');
        resultInfo.innerHTML = '';
        
        const linkContainer = document.createElement('div');
        linkContainer.className = 'link-container';
        
        const linkEl = document.createElement('a');
        linkEl.href = url;
        linkEl.target = '_blank';
        linkEl.textContent = url;
        
        const copyBtn = document.createElement('button');
        copyBtn.className = 'copy-btn';
        copyBtn.textContent = '复制';
        copyBtn.addEventListener('click', function() {
            navigator.clipboard.writeText(url).then(() => {
                copyBtn.textContent = '已复制';
                setTimeout(() => {
                    copyBtn.textContent = '复制';
                }, 2000);
            });
        });
        
        linkContainer.appendChild(linkEl);
        linkContainer.appendChild(copyBtn);
        
        resultInfo.appendChild(linkContainer);
    }
}

// 更新上传失败结果
function updateResultError(fileName, errorMsg) {
    const resultItem = document.querySelector(`.result-item[data-name="${fileName}"]`);
    if (resultItem) {
        resultItem.className = 'result-item error';
        
        const resultInfo = resultItem.querySelector('.result-info');
        resultInfo.textContent = errorMsg;
        resultInfo.className = 'result-info error-text';
    }
} 