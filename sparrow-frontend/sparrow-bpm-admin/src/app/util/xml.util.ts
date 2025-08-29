export function getProcessName(xmlString: string) {
    const parser = new DOMParser();
    const xmlDoc = parser.parseFromString(xmlString, "application/xml");

    // 取第一个 process 节点
    const processEl = xmlDoc.getElementsByTagName("bpmn2:process")[0];

    // 获取 name 属性
    const processName = processEl.getAttribute("name");
    return processName; // 输出：流程名称: 备料分析

    // const books = xmlDoc.getElementsByTagName("bpmn2:process");
    // for (let i = 0; i < books.length; i++) {
    //     const title = books[i].getElementsByTagName("title")[0].textContent;
    //     const author = books[i].getElementsByTagName("author")[0].textContent;
    //     console.log(`Title: ${title}, Author: ${author}`);
    // }
}