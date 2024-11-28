const express = require('express');
const multer = require('multer');
const path = require('path');
const fs = require('fs');

// Configuración del almacenamiento de Multer
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    // Definir la carpeta de destino para almacenar las imágenes
    const uploadDir = 'uploads/';
    // Crear la carpeta si no existe
    if (!fs.existsSync(uploadDir)) {
      fs.mkdirSync(uploadDir);
    }
    cb(null, uploadDir);
  },
  filename: (req, file, cb) => {
    // Definir el nombre del archivo guardado
    cb(null, Date.now() + path.extname(file.originalname));  // Timestamp + extensión del archivo
  }
});

// Inicializar multer con la configuración de almacenamiento
const upload = multer({ storage: storage });

const app = express();

// Ruta para manejar la carga de la imagen
app.post('/upload', upload.single('image'), (req, res) => {
  // Si no se ha recibido archivo
  if (!req.file) {
    return res.status(400).send('No se ha enviado ningún archivo');
  }

  console.log('Imagen recibida:', req.file);

  // Responder con el nombre del archivo cargado y la ruta
  res.status(200).json({
    message: 'Imagen cargada con éxito',
    file: req.file
  });
});

// Configurar el servidor para escuchar en un puerto
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Servidor corriendo en http://localhost:${PORT}`);
});