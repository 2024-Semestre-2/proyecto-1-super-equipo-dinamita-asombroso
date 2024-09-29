MOV DX, 65
INT _10H
INT _09H
MOV BX, 100
INT _08H
MOV AX, 0
INT _21H
MOV AX, 1
INT _21H
MOV AX, 2
INT _21H
INT _20H
// Este archivo debería ser aceptado por el Assembler