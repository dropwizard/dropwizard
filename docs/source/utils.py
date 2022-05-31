import xml.dom.minidom

def child_node(node, name):
    for i in node.childNodes:
        if (i.nodeType == i.ELEMENT_NODE) and (i.tagName == name):
            return i
    return None


def node_text(node):
    return node.childNodes[0].data


def maven_version(pom):
    dom = xml.dom.minidom.parse(pom)
    project = dom.childNodes[0]

    version = child_node(project, 'version')
    if version:
        return node_text(version)

    parent = child_node(project, 'parent')
    version = child_node(parent, 'version')
    return node_text(version)
