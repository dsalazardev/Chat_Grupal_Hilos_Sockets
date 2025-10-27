# Chat Grupal Concurrente con Sockets y Hilos en Python

## Descripción

Este proyecto, desarrollado para la materia de Programación Concurrente y Distribuida, implementa un chat grupal multicliente. Utiliza una arquitectura Cliente-Servidor clásica, demostrando la gestión de múltiples conexiones simultáneas mediante hilos y la sincronización de recursos compartidos en Python.

## Conceptos Demostrados

  * **Sockets TCP (`AF_INET`, `SOCK_STREAM`)**: La comunicación se basa en sockets TCP para garantizar una conexión fiable, orientada a la conexión y ordenada entre el servidor y los múltiples clientes.
  * **Programación Orientada a Objetos (POO)**: El código está estructurado en clases (`ChatServer`, `ClientHandler`, `ChatClient`) para encapsular la lógica, gestionar el estado y facilitar la mantenibilidad del servidor y el cliente.
  * **Hilos (`threading.Thread`)**: El servidor es multihilo. Utiliza la herencia, creando una subclase `ClientHandler` que hereda de `threading.Thread`. Esto permite que cada cliente conectado sea manejado por un hilo independiente, permitiendo al servidor aceptar nuevas conexiones sin bloquearse.
  * **Sincronización (`threading.Lock`)**: Se utiliza un `threading.Lock` para proteger el recurso compartido (`self.clients` del servidor), que es una sección crítica. Esto es vital para prevenir condiciones de carrera al añadir/eliminar clientes o al iterar la lista para un `broadcast`.

## Arquitectura y Funcionamiento

### 1\. Servidor (chat\_server.ipynb)

  * **Clase `ChatServer`**: Actúa como el orquestador principal. Su rol es inicializar el socket (`socket.socket`), enlazarlo a una dirección y puerto (`bind`), y ponerlo en modo de escucha (`listen`). Su método `run()` contiene el bucle principal que espera y acepta nuevas conexiones (`accept()`).
  * **Clase `ClientHandler`**: Se crea una instancia de esta clase (que es un Hilo) por cada cliente que se conecta. Su método `run()` maneja el ciclo de vida completo de ese cliente:
    1.  Espera recibir un primer mensaje que usa como `nombre_usuario`.
    2.  Anuncia la conexión del usuario al resto del chat.
    3.  Entra en un bucle para recibir y reenviar (`broadcast`) todos los mensajes de chat de ese cliente.
    4.  Maneja la desconexión (limpia o abrupta) usando un bloque `finally` para cerrar la conexión y eliminar al cliente de la lista.
  * **Énfasis en Sincronización**: Los métodos `add_client`, `remove_client` y `broadcast` usan `with self.clients_lock:`. Esto garantiza la **exclusión mutua**: solo un hilo puede acceder a la lista `self.clients` a la vez, asegurando su integridad y evitando errores al modificarla e iterarla simultáneamente.

### 2\. Cliente (chat\_cliente.ipynb)

  * **Clase `ChatClient`**: Su rol es encapsular toda la lógica del cliente. En su método `start()`, se conecta al servidor (`connect()`), pide el `nombre_usuario` al usuario y lo envía inmediatamente al servidor para identificarse.
  * **Énfasis en Concurrencia**: El cliente logra una comunicación *full-duplex* (simulada) de manera eficiente. Lanza un hilo secundario (`receiver_thread`) que ejecuta el método `_listen_for_messages`. Este hilo se dedica exclusivamente a esperar (`recv()`) y mostrar mensajes del servidor. Mientras tanto, el hilo principal ejecuta `_send_messages_loop`, dedicándose a esperar la entrada del usuario (`input()`). Esto evita que el `input()` (bloqueante) impida recibir mensajes, y que `recv()` (bloqueante) impida al usuario escribir.

## Instrucciones de Uso

1.  Abre una terminal y ejecuta el servidor:

    ```bash
    python chat_server.ipynb
    ```

    *El servidor confirmará que está escuchando en localhost:8070.*

2.  Abre una **nueva terminal** (o varias) y ejecuta el cliente:

    ```bash
    python chat_cliente.ipynb
    ```

    *Se te pedirá tu nombre de usuario.*

3.  Repite el paso 2 para cada usuario adicional que quieras conectar al chat.

4.  Escribe `salir` en cualquier cliente para desconectarte.

## Autores

*Daner Alejandro Salazar Colorado*

*Jaime Andres Cardona Diaz*

Ingeniería de Sistemas y Computación