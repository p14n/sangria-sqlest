// Execute the following steps on the master
node('master') {
   //git url: 'https://github.com/jenkinsbyexample/workflow-plugin-pipeline-demo.git'
   checkout scm
   compileTest()
}

functionalTest()
release()

def functionalTest(){

    stage name: 'QA', concurrency: 1

    parallel(rest153: {
        echo "rest153"
    },rest154: {
        echo "rest154"
    },restF63: {
        echo "restF63"
    },nonrest153: {
        echo "nonrest153"
    },nonrest154: {
        echo "nonrest154"
    },nonrestF63: {
        echo "nonrestF63"
    })
    
}

def compileTest() {

    // Execute maven build and archive artifacts
    stage 'Build'
    echo "Compile and build"
    //sbt -Dsbt.log.noformat=true -Xmx2G -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=2G -Xss2M clean update compile test it:compile generateXsd package vertxMod
 
}

def release() {

    input message: "Release this commit point?"

    stage name: 'Release', concurrency: 1
    node('master') {
        //sbt release
        echo "Released"

    }
}
