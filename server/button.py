import RPi.GPIO as GPIO
import time

GPIO.setmode(GPIO.BCM)

GPIO.setup(19, GPIO.IN, pull_up_down=GPIO.PUD_UP) #Button to GPIO 19
GPIO.setup(18, GPIO.OUT, initial=0)               #LED to GPIO 18
GPIO.setup(23, GPIO.OUT, initial=0)               #LED to GPIO 23

from pygame import mixer
mixer.init()

try:
    buttonPressed = False
    lightOn = False

    while True:
        button_state = GPIO.input(19)
        if button_state == False:
            buttonPressed = True
            #GPIO.output(23, True)
            #print('Button Pressed...')
            time.sleep(0.2)
        else:
            if buttonPressed == True:
                buttonPressed = False
                lightOn = not lightOn
                GPIO.output(23, lightOn)
                GPIO.output(18, not lightOn)
                #alert = mixer.Sound('sound/bell.wav')
                #alert.play()
except:
    print "Error occurred!"
finally:
    print "Cleaning up"
    GPIO.cleanup()
