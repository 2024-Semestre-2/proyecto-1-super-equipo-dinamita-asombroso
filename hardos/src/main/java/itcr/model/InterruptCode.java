package itcr.model;

public enum InterruptCode {
  _08H, // pide cadena *guarda en memoria* -> DX (direccion de memoria)
  _09H, // pide numero 0-255 -> DX
  _10H, // imprime DX
  _20H, // termina proceso
  _21H, // gestion de archivos
}