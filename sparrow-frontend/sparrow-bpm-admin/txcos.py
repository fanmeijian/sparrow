# -*- coding=utf-8
from qcloud_cos import CosConfig
from qcloud_cos import CosS3Client
from qcloud_cos import CosServiceError
from qcloud_cos import CosClientError
import os
from dotenv import load_dotenv
from pathlib import Path
import sys
import argparse
import logging

from datetime import datetime

# 基础图片处理相关API 请参考 https://cloud.tencent.com/document/product/460/36540


def upload_folder_to_cos(local_folder_path, bucket_name, cos_folder_prefix=''):
    """
    上传本地目录到COS
    :param local_folder_path: 本地文件夹路径
    :param bucket_name: 存储桶名称
    :param cos_folder_prefix: COS中对应的目标文件夹前缀
    """
    for root, dirs, files in os.walk(local_folder_path):
        for file in files:
            # 获取本地文件全路径
            local_file_path = os.path.join(root, file)

            # 计算COS上的Key（相对路径）
            relative_path = os.path.relpath(local_file_path, local_folder_path)
            cos_key = os.path.join(
                cos_folder_prefix, relative_path).replace('\\', '/')

            print(f'正在上传: {local_file_path} -> {cos_key}')

            # 2. 上传文件 (推荐使用高级接口上传)
            client.upload_file(
                Bucket=bucket_name,
                LocalFilePath=local_file_path,
                Key=cos_key,
                PartSize=1,           # 设置分块大小
                MAXThread=10,         # 设置最大线程数
                EnableMD5=False       # 是否开启MD5校验
            )


def percentage(consumed_bytes, total_bytes):
    """进度条回调函数，计算当前上传的百分比

    :param consumed_bytes: 已经上传/下载的数据量
    :param total_bytes: 总数据量
    """
    if total_bytes:
        rate = int(100 * (float(consumed_bytes) / float(total_bytes)))
        print('\r{0}% '.format(rate))
        sys.stdout.flush()


def delete_cos_folder(bucket, folder_path):
    """
    删除 COS 上的“文件夹”及其内部所有内容
    :param folder_path: 文件夹路径，例如 'test/data/'
    """
    # 确保路径以 / 结尾，避免误删前缀相似的文件夹（如 test/data1）
    if not folder_path.endswith('/'):
        folder_path += '/'

    # 递归获取目录下所有对象
    is_truncated = True
    marker = ''

    while is_truncated:
        # 列出该前缀下的所有对象
        response = client.list_objects(
            Bucket=bucket,
            Prefix=folder_path,
            Marker=marker
        )

        if 'Contents' in response:
            # 提取所有待删除的 Key
            delete_list = [{'Key': obj['Key']} for obj in response['Contents']]

            # 执行批量删除
            delete_response = client.delete_objects(
                Bucket=bucket,
                Delete={'Object': delete_list}
            )
            print(f"已删除 {len(delete_list)} 个对象")

        # 检查是否还有更多文件（分页）
        is_truncated = response.get('IsTruncated') == 'true'
        marker = response.get('NextMarker')

    print(f"文件夹 {folder_path} 已清空并删除。")


def upload(file_path, key):
    # 腾讯云COSV5Python SDK, 目前可以支持Python2.6与Python2.7以及Python3.x

    # pip安装指南:pip install -U cos-python-sdk-v5

    # cos最新可用地域,参照https://www.qcloud.com/document/product/436/6224

    logging.basicConfig(level=logging.INFO, stream=sys.stdout)

    response = client.put_object_from_local_file(
        Bucket='zhanhun-1398580572',
        LocalFilePath=file_path,
        Key=key,
    )
    print(response['ETag'])


# 使用示例
if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='上传本地文件夹到腾讯云COS')
    parser.add_argument('local_path', help='本地文件夹路径')
    parser.add_argument('bucket', help='存储桶名称')
    parser.add_argument('cos_folder', help='COS目标文件夹前缀')
    parser.add_argument('-e', '--env-path', help='.env 配置文件路径', required=True)
    args = parser.parse_args()

    local_folder_path = args.local_path
    bucket_name = args.bucket
    cos_folder_prefix = args.cos_folder
    env_path = Path(args.env_path)

    # 加载指定路径的环境变量文件
    if env_path.exists():
        load_dotenv(dotenv_path=env_path, verbose=True)
        print(f"成功加载配置: {env_path}")
    else:
        print(f"未找到配置文件: {env_path}")
        print("SECRET_ID=")
        print("SECRET_KEY=")
    # 设置用户属性, 包括 secret_id, secret_key, region等。Appid 已在CosConfig中移除，请在参数 Bucket 中带上 Appid。Bucket 由 BucketName-Appid 组成
    # 替换为用户的 SecretId，请登录访问管理控制台进行查看和管理，https://console.cloud.tencent.com/cam/capi
    secret_id = os.getenv("SECRET_ID")
    # 替换为用户的 SecretKey，请登录访问管理控制台进行查看和管理，https://console.cloud.tencent.com/cam/capi
    secret_key = os.getenv("SECRET_KEY")
    # 替换为用户的 region，已创建桶归属的region可以在控制台查看，https://console.cloud.tencent.com/cos5/bucket
    region = 'ap-guangzhou'
    # COS支持的所有region列表参见https://www.qcloud.com/document/product/436/6224
    # 如果使用永久密钥不需要填入token，如果使用临时密钥需要填入，临时密钥生成和使用指引参见https://cloud.tencent.com/document/product/436/14048
    token = None
    domain = None  # domain可以不填，此时使用COS区域域名访问存储桶。domain也可以填写用户自定义域名，或者桶的全球加速域名
    # 填写用户自定义域名，比如user-define.example.com，需要先开启桶的自定义域名，具体请参见https://cloud.tencent.com/document/product/436/36638
    # 填写桶的全球加速域名，比如examplebucket-1250000000.cos.accelerate.tencentcos.cn，需要先开启桶的全球加速功能，请参见https://cloud.tencent.com/document/product/436/38864

    config = CosConfig(Region=region, SecretId=secret_id,
                       SecretKey=secret_key, Token=token, Domain=domain)  # 获取配置对象
    client = CosS3Client(config)
    # 本地路径 简单上传

    delete_cos_folder(
        bucket=bucket_name,
        folder_path=cos_folder_prefix
    )

    upload_folder_to_cos(
        local_folder_path=local_folder_path,
        bucket_name=bucket_name,
        cos_folder_prefix=cos_folder_prefix
    )
