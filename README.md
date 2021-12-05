# react-native-blueprint-label

## Getting started

`$ npm install react-native-blueprint-label --save`

## Usage
```javascript
import ReactNativeBlueprintLabel from 'react-native-blueprint-label';

// TODO: What to do with the module?
ReactNativeBlueprintLabel;
```

### Manual Linking 
1. Add this in src/build.gradle file
```
implementation project(':react-native-blueprint-label')
```

2. Add this in settings.gradle file
```
include ':react-native-blueprint-label'
project(':react-native-blueprint-label').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-blueprint-label/android')
```

3. Add this in MainApplication.java file
```
import com.blueprinter.ReactNativeBlueprintLabelPackage; -> On top
new RNBluetoothEscposPrinterPackage() -> On Get packages
```

### Usage 

```
import ReactNativeBlueprintLabel from 'react-native-blueprint-label';
//Init printer module
ReactNativeBlueprintLabel.init();

//Get the devices
ReactNativeBlueprintLabel.getDevices()
//It will return [{'Address=BTName'}]

//Connect the devices
ReactNativeBlueprintLabel.connectDevice(btAddress);

//If you want open blackmark sensor in blue printer
ReactNativeBlueprintLabel.openBlackMark()

//If you want close blackmark sensor in blue printer
ReactNativeBlueprintLabel.closeBlackMark()

//Print Text
ReactNativeBlueprintLabel.printText('Your Text')

//Print Paragraph
const data = [
    'Queue: -               (1/1)',
    'Customer: -                 ',
    '----------------------------',
    'Bombay, ABahan1             ',
    '   1                        ',
    '   2                        ',
    '   3                        ',
    '   4                        ',
    '   5                        ',
  ]
ReactNativeBlueprintLabel.printParagraph(data)
```




