package pw.binom.sceneEditor

import java.net.URLClassLoader

class NodeClassLoader(parent: ClassLoader) : URLClassLoader(emptyArray(), parent) {
}