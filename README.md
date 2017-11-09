# Smart Cup Mat

Temperature of coffee or tea sometimes is too high for some people. Our Smart cup mat can solve this problem. 
Smart cup mat is a device which could detect the temperature of water in a cup and let the user know what the 
temperature is in there through mobilephone. User can also set a specific temperature they like. When the 
temperature of setting is same as the water, system will sent out notification to user. We build the system 
with RedBearLab nRF51822 BLE nano kit, Analog Devices TMP 36 temperature sensor and an App on android phone. 
With a capacity of 3.3V, the RF peek current is around 10 mA. Overall power of the system is pretty low. 


## Whole system image

System contains a cup mat, a BLE(Bluetooth Low Energy) board, a temperature sensor, a battery and an android phone. 

<img src="https://github.com/Xiaoxia-Zheng/SmartCupMat/blob/master/Sys_Image/system.jpg" alt="Smiley face" height="300">



### BLE nano kit

RedBeaLab nRF51822 BLE nano kit is being used. It includes microcontroller, ADC and BLE module all in one. 
The analog pin, A3, is used to connect to the sensor’s analog output and receive data.

<img src="https://github.com/Xiaoxia-Zheng/SmartCupMat/blob/master/Sys_Image/BLEnano.png" alt="Smiley face" height="150">
<img src="https://github.com/Xiaoxia-Zheng/SmartCupMat/blob/master/Sys_Image/BLEpin.png" alt="Smiley face" height="150">





### Android APP

Android Studio is used to build the android app in this project. In the main acitivity of the APP, users can set their 
desired temperature for notification. The realtime temperature of the cup mat is also updated. Once the realtime temperature 
reach the same as setting temperature, the APP will sent out an notification for the user.

<img src="https://github.com/Xiaoxia-Zheng/SmartCupMat/blob/master/Sys_Image/appScreen.png" alt="Smiley face" height="400">




## Demo video
https://www.youtube.com/watch?v=hgKJ4Mw1bTk&t=31s
