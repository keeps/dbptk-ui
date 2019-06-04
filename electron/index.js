var title = 'Title';
var port = 8080;
var windowWidth = 1200;
var windowHeight = 800;
var animationWidth = 300;
var animationHeight = 150;
var windowsJavaPath = 'java.exe';
var darwinJavaPath = 'java';


const {
    app, session, protocol, BrowserWindow, Menu, globalShortcut
} = require('electron');
const path = require('path');
var waitOn = require('wait-on');
var tmp = require('tmp');

let mainWindow = null;
let serverProcess = null;



killTree = function () {
    console.log('Kill server process ' + serverProcess.pid);
    require('tree-kill')(serverProcess.pid, "SIGTERM", function (err) {
        console.log('Server process killed');
    });
}

var otherInstanceOpen = !app.requestSingleInstanceLock();
app.on('second-instance', function (event, commandLine, workingDirectory) {
    if (mainWindow) {
        if (mainWindow.isMinimized()) mainWindow.restore();
        mainWindow.show();
        mainWindow.focus();
    }
    return true;
});

if (otherInstanceOpen) {
    console.log("Already open...")
    app.quit();
    return;
}
var fs = require('fs');
var files = fs.readdirSync(app.getAppPath() + '/war');
var filename = null;
for (var i in files) {
    if (path.extname(files[i]) === '.war') {
        filename = path.basename(files[i]);
        break;
    }
}
if (!filename) {
    setTimeout(function () {
        app.exit()
    }, 1000);
    throw new Error('The Application could not be started');
}
// Provide API for web application
global.callElectronUiApi = function (args) {
    console.log('Electron called from web app with args ' + args);
    if (args) {
        if (args[0] === 'exit') {
            console.log('Kill server process');
            const kill = require('tree-kill');
            kill(serverProcess.pid, 'SIGTERM', function (err) {
                console.log('Server process killed');
                serverProcess = null;
                mainWindow.close();
            });
        }
        if (args[0] === 'minimize') {
            mainWindow.minimize();
        }
        if (args[0] === 'maximize') {
            if (!mainWindow.isMaximized()) {
                mainWindow.maximize();
            }
            else {
                mainWindow.unmaximize();
            }
        }
    }
};
app.on('window-all-closed', function () {
    app.quit();
});

app.on('ready', async function () {

    // Loading
    let loading = new BrowserWindow({
        show: false
        , frame: false
        , title: title
        , width: animationWidth
        , height: animationHeight
    });
    console.log(app.getAppPath());
    loading.loadURL("file://" + app.getAppPath() + '/loading.html');
    loading.show();
    loading.webContents.once('dom-ready', () => {
        console.log("Loading...")
        loading.show();
    });


    // Spawn Java process
    platform = process.platform;
    var javaPath = 'java';
    if (platform === 'win32') {
        javaPath = windowsJavaPath;
    }
    else if (platform === 'darwin') {
        javaPath = darwinJavaPath;
    }
    const { spawn } = require('child_process');

    var serverPortFile = tmp.tmpNameSync();
    console.log("Port file at " + serverPortFile);

    var jvmLog = tmp.tmpNameSync();
    console.log("JVM log at " + jvmLog);

    // Ask for a random unassigned port and to write it down in serverPortFile
    var javaVMParameters = ["-Dserver.port=0", "-Dserver.port.file=" + serverPortFile];

    serverProcess = spawn(javaPath, ['-jar'].concat(javaVMParameters).concat("war/" + filename), {
        cwd: app.getAppPath().replace('app.asar', 'app.asar.unpacked') + '/'
    });
    serverProcess.stdout.pipe(fs.createWriteStream(jvmLog, {
        flags: 'a'
    })); // logging
    serverProcess.on('error', (code, signal) => {
        setTimeout(function () {
            app.exit()
        }, 1000);
        throw new Error('The Application could not be started');
    });
    console.log('Server PID: ' + serverProcess.pid);

    // Waiting for app to start
    console.log('Wait until ' + serverPortFile + ' exists...');
    await waitOn({ resources: [serverPortFile] });

    port = parseInt(fs.readFileSync(serverPortFile));
    fs.unlink(serverPortFile, (err) => { });

    let appUrl = 'http://localhost:' + port;

    console.log("Server at " + appUrl);
    await waitOn({ resources: [appUrl] });
    console.log('Server started!');

    // Open window with app
    mainWindow = new BrowserWindow({
        title: title
        , width: windowWidth
        , height: windowHeight
        , frame: true
    });
    mainWindow.loadURL(appUrl + "/?branding=false");
    mainWindow.webContents.once('dom-ready', () => {
        console.log('main loaded')
        mainWindow.show()
        loading.hide()
        loading.close()
    })
    mainWindow.on('closed', function () {
        mainWindow = null;
    });
    mainWindow.on('close', function (e) {
        if (serverProcess) {
            var choice = require('electron').dialog.showMessageBox(this, {
                type: 'question'
                , buttons: ['Yes', 'No']
                , title: 'Confirm'
                , message: 'Dou you really want to exit?'
            });
            if (choice == 1) {
                e.preventDefault();
            }
        }
    });


    // Register a shortcut listener.
    const ret = globalShortcut.register('CommandOrControl+Shift+`', () => {
        console.log('Bring to front shortcut triggered');
        if (mainWindow) {
            mainWindow.focus();
        }
    })
});
app.on('will-quit', (event) => {
    if (serverProcess != null) {
        event.preventDefault();

        // Unregister all shortcuts.
        globalShortcut.unregisterAll();

        console.log('Kill server process ' + serverProcess.pid);

        require('tree-kill')(serverProcess.pid, "SIGTERM", function (err) {
            console.log('Server process killed');
            serverProcess = null;
            app.quit();
        });
    }
});