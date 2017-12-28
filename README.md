# speech-test

Simple app that reads from the mic, sends to the Speech API and prints the text spoken.  

Works very accurately, it's astonishing!  

Speak "exit" to exit.  

## setup

Add a keys json file to `src/main/resources`. Get one following these steps:

 * create a project in google cloud
 * enable Speech API
 * create a new Service Account with at least Viewer privileges (that's what I tried)
 * select JSON and download the file
 * move it to `src/main/resources/speech-test-key.json` (it's already gitignored)

That's it!
