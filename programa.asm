; Código Assembly Gerado para Mini-Linguagem Portugol (NASM x86)

section .data
_num_format db "%d", 0
_temp_read_buffer resb 32
a: dd 0
T1: dd 0
T2: dd 0

section .text
global _start
extern printf, scanf

_start:

; C3E: contador = 5
MOV EAX, 5
MOV [contador], EAX

; C3E: limite = 10
MOV EAX, 10
MOV [limite], EAX

; C3E: resultado = 0
MOV EAX, 0
MOV [resultado], EAX

; C3E: L1:
L1:

; C3E: T1 = contador CMPLE limite
MOV EAX, [contador]
CMP EAX, [limite]
XOR EAX, EAX
SETLE AL
MOV [T1], EAX

; C3E: JMPFALSE L2 T1
MOV EAX, [T1]
CMP EAX, 0
JE L2

; C3E: T2 = contador * 2
MOV EAX, [contador]
IMUL DWORD [2]
MOV [T2], EAX

; C3E: T3 = resultado + T2
MOV EAX, [resultado]
ADD EAX, [T2]
MOV [T3], EAX

; C3E: T4 = T3 / taxa
MOV EAX, [T3]
XOR EDX, EDX
IDIV DWORD [taxa]
MOV [T4], EAX

; C3E: resultado = T4
MOV EAX, [T4]
MOV [resultado], EAX

; C3E: JMP L1
JMP L1

; C3E: L2:
L2:

; C3E: T5 = contador RESTO 3
MOV EAX, [contador]
XOR EDX, EDX
IDIV DWORD [3]
MOV EAX, EDX
MOV [T5], EAX

; C3E: contador = T5
MOV EAX, [T5]
MOV [contador], EAX

; C3E: T6 = contador CMPEQ 0
MOV EAX, [contador]
CMP EAX, 0
XOR EAX, EAX
SETE AL
MOV [T6], EAX

; C3E: JMPFALSE L3 T6
MOV EAX, [T1]
CMP EAX, 0
JE L2

; C3E: T7 = taxa + 1
MOV EAX, [taxa]
ADD EAX, 1
MOV [T7], EAX

; C3E: taxa = T7
MOV EAX, [T7]
MOV [taxa], EAX

; C3E: L3:
L3:

; C3E: T8 = resultado CMPGT 100
MOV EAX, [resultado]
CMP EAX, 100
XOR EAX, EAX
SETG AL
MOV [T8], EAX

; C3E: JMPFALSE L5 T8
MOV EAX, [T1]
CMP EAX, 0
JE L2

; C3E: resultado = 0
MOV EAX, 0
MOV [resultado], EAX

; C3E: JMP L6
JMP L6

; C3E: L5:
L5:

; C3E: T9 = resultado + 1
MOV EAX, [resultado]
ADD EAX, 1
MOV [T9], EAX

; C3E: resultado = T9
MOV EAX, [T9]
MOV [resultado], EAX

; C3E: L6:
L6:

; Finaliza o programa (System call exit)
MOV EAX, 1
XOR EBX, EBX
INT 0x80
