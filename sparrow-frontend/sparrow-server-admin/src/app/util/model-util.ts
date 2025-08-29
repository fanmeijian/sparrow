/**
 * 用于仅展示基本内容, 通常用于FORM表单的展示
 * @param id
 * @returns
 */

export const showId = (id: any) => {
  if (typeof id === 'object') {
    return JSON.stringify(id)
  } else {
    return id
  }

}

export const showContent = (_content: any) => {
  if (typeof _content === 'object') {
    let content = Object.assign({}, _content)
    delete content.id
    delete content.createdDate
    delete content.createdBy
    delete content.modifiedDate
    delete content.modifiedBy
    delete content.version
    delete content.stat
    delete content.modelName
    delete content.entityStat
    delete content.enabled
    delete content.dataPermissionTokenId
    delete content.dataPermissionId
    delete content.dataPermission
    delete content.errMsgs
    delete content.permissionTokenId

    return JSON.stringify(content)
  } else {
    return _content
  }
}
