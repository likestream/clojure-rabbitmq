from fabric.api import *
from fabric.contrib.project import *

@hosts('maven.likestream.net')
def deploy():
    "puts jar and pom into local maven repo"
    sudo('rm /home2/maven/maven2/clojure-rabbitmq/clojure-rabbitmq/0.3.3-SNAPSHOT/clojure-rabbitmq-0.3.3-SNAPSHOT.pom')
    sudo('rm /home2/maven/maven2/clojure-rabbitmq/clojure-rabbitmq/0.3.3-SNAPSHOT/clojure-rabbitmq-0.3.3-SNAPSHOT.jar')    
    put('pom.xml', '/home2/maven/maven2/clojure-rabbitmq/clojure-rabbitmq/0.3.3-SNAPSHOT/clojure-rabbitmq-0.3.3-SNAPSHOT.pom')
    put('clojure-rabbitmq-0.3.3-SNAPSHOT.jar', '/home2/maven/maven2/clojure-rabbitmq/clojure-rabbitmq/0.3.3-SNAPSHOT')
    sudo('chown -R maven:daemon /home2/maven/maven2/clojure-rabbitmq/clojure-rabbitmq/0.3.3-SNAPSHOT')
    
def mktags():
    "generates emacs tag file for subdirectory"
    local('exuberant-ctags -e -f TAGS -R .')


    



    
