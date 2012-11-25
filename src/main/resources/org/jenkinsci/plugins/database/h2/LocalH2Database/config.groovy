package org.jenkinsci.plugins.database.h2.LocalH2Database

def f = namespace(lib.FormTagLib)

f.entry(field:"path",title:_("File Path")) {
    f.textbox()
}