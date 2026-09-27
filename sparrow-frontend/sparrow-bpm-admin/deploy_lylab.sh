#!/bin/bash
# source $HOME/.pyenv/versions/karaok/bin/activate

# 1. 获取脚本所在的绝对路径
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)

# 2. 获取该路径的最后一个目录名 (basename)
PROJECT_NAME=$(basename "$SCRIPT_DIR")

# 切换到脚本目录
cd "$SCRIPT_DIR" || exit

npx ng build --base-href="/$PROJECT_NAME/"

# 运行 Python 脚本
# 这里的参数根据你的逻辑拼接：./dist/项目名 桶名 项目名
python txcos.py -e ~/.txcos/.env_lylab "./dist/$PROJECT_NAME" dengbo-1305398251 "$PROJECT_NAME"
