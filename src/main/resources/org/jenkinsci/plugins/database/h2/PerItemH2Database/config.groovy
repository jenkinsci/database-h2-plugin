package org.jenkinsci.plugins.database.h2.PerItemH2Database

def f = namespace(lib.FormTagLib)
f.advanced(){
    f.entry(field:"autoServer",title:_("Use Automatic Mixed Mode")) {
        f.checkbox()
    }
}
