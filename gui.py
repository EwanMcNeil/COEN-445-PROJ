import subprocess

import os
import signal
import subprocess
import PySimpleGUI as sg

layout = [[sg.Text("Hello from PySimpleGUI")], [sg.Button("Register")]]

# Create the window
window = sg.Window("Demo", layout)

process = subprocess.Popen('java -jar Client.jar localhost 10011 localhost 10012',shell = True, stdin = subprocess.PIPE)

# stdout=subprocess.PIPE, stdin=subprocess.PIPE, 
 #   stderr=subprocess.PIPE)


# Create an event loop
while True:
    event, values = window.read()
    # End program if user closes window or
    # presses the OK button
    if event == "Register":
     # print('\n')
      process.communicate(input =b'register')
       #process.communicate(input='\n')
      # print("register")
      
       
    if event == sg.WIN_CLOSED:
        os.killpg(os.getpgid(process.pid), signal.SIGTERM)
window.close()

