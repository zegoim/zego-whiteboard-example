import os
import json
import zipfile
from zegopy.common import log
from zegopy.common import command
from zegopy.builder import oss_helper
from datetime import datetime


# 直接上传文件到阿里云
def upload_to_oss_with_url(local_file_path: str, oss_file_path: str):
    """根据传入的本地文件路径和OSS相对文件夹路径上传阿里云OSS

    示例：
        local_file_path: /Users/zego/test.txt
        oss_file_path: downloads/contents/new_test.txt
        上传结果: https://storage.zego.im/downloads/contents/new_test.txt

    Args:
        local_file_path (str): 待上传的文件的绝对路径
        oss_file_path (str): 将要上传到 OSS 的路径（末尾文件名若与源文件名不一致会以此参数的文件名为准重命名）
        tool (str): 使用哪个上传工具，参考此脚本最上面的注释，默认使用 ossr

    Returns:
        OSS 目标 URL
    """
    helper = oss_helper.OssHelper()
    helper.set_auth(os.environ.get('oss_access_key_id'), os.environ.get('oss_access_key_secret'))
    helper.set_bucket('zego-public')
    helper.upload_fie(local_file_path, oss_file_path)
    return 'https://storage.zego.im/{}'.format(oss_file_path)


def zipdir(path, target_file):
    zipf = zipfile.ZipFile(target_file, 'w', zipfile.ZIP_DEFLATED)
    # zipf is zipfile handle
    for root, dirs, files in os.walk(path):
        for file in files:
            zipf.write(os.path.join(root, file),
                       os.path.relpath(os.path.join(root, file),
                                       os.path.join(path, '..')))
    zipf.close()


if __name__ == "__main__":
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    zip_file = 'zego-whiteboard-example-{}.zip'.format('express')
    command.execute_and_print('mkdir zego-whiteboard-example;cp -r docs src LICENSE README.md zego-whiteboard-example')
    zipdir('zego-whiteboard-example', zip_file)

    oss_full_url = upload_to_oss_with_url(zip_file, 'github/zego-whiteboard-example/{}'.format(zip_file))
    print("Upload to OSS finished: ", oss_full_url)
