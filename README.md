# PhotoPicker

Here is the Demo for the PhotoPicker in All Android Versions

I have added other pickers as well(pdf,doc,video,pictures).

I have tested with multiple scenarios , here is the scenario list

1. Android 8 to 13 versions
2. Also Add the Single Photo and Multiple Photo Selection
3. Single Photo Selection Working on All Android Versions
4. Multiple Photo Selection will work from Android 11 to Android 13.
5. Multiple Photo Selection below Android 11 we have to use clipdata the old way.

Here how to use library in your project follow below steps .

1. In your gradle(app) add below line
```
    dependencies{
         implementation 'com.github.ParthLotia:FilePicker:Tag'
    }
```   

2. In your project level gradle add below line
```
    repositories {
        google()
        mavenCentral()
        maven { url "https://www.jitpack.io" }
    }
```


<!--![LocationPermission](art/ss_location1.png)-->

<p align="center">
  <img src="art/ss_photopicker1.png" width="350">
  <img src="art/ss_photopicker2.png" width="350">
  <img src="art/ss_photopicker3.png" width="350">
</p>
