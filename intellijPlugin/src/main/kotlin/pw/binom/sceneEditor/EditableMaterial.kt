package pw.binom.sceneEditor

import mogot.Material

interface EditableMaterial : Material {
    var hover: Boolean
    var selected: Boolean
}