import React, { useEffect, useState } from 'react';
import axios from 'axios';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import { useDispatch, useSelector } from 'react-redux';
import { setFiles } from './store/filesSlice';

const ALLOWED_TYPES = ['text/plain', 'image/jpeg', 'image/png', 'application/json'];

const App = () => {
  const dispatch = useDispatch();
  const files = useSelector(state => state.files.items);
  const [selectedFile, setSelectedFile] = useState(null);

  useEffect(() => {
    fetchFiles();
  }, []);

  const fetchFiles = async () => {
    try {
      const res = await axios.get('http://localhost:8080/api/files/list');
      dispatch(setFiles(res.data));
    } catch (error) {
      console.error('Error fetching files:', error);
      // Initialize with empty array if backend is not available
      dispatch(setFiles([]));
    }
  };

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file && ALLOWED_TYPES.includes(file.type)) {
      setSelectedFile(file);
    } else {
      alert('Unsupported file type. Only txt, jpg, png, json allowed.');
    }
  };

  const handleUpload = async () => {
    if (!selectedFile) return;
    const formData = new FormData();
    formData.append('file', selectedFile);
    await axios.post('http://localhost:8080/api/files/upload', formData);
    fetchFiles();
    setSelectedFile(null);
  };

  const handleDownload = async (filename) => {
    const res = await axios.get(`http://localhost:8080/api/files/download/${filename}`, { responseType: 'blob' });
    const url = window.URL.createObjectURL(new Blob([res.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', filename);
    document.body.appendChild(link);
    link.click();
    link.remove();
  };

  const handleView = async (filename) => {
    const res = await axios.get(`http://localhost:8080/api/files/download/${filename}`, { responseType: 'blob' });
    const blob = new Blob([res.data]);
    const url = window.URL.createObjectURL(blob);
    window.open(url, '_blank');
  };

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-4">My Files</h1>
      <div className="mb-4 flex gap-2">
        <input
          type="file"
          onChange={handleFileChange}
          style={{ padding: '10px', border: '1px solid #ccc', borderRadius: '4px' }}
        />
        <Button variant="contained" onClick={handleUpload} disabled={!selectedFile}>Upload</Button>
      </div>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {files.map(file => (
          <Card key={file} className="p-4 flex flex-col gap-2">
            <CardContent className="flex flex-col">
              <span className="font-medium">{file}</span>
              <div className="mt-2 flex gap-2">
                <Button onClick={() => handleDownload(file)}>Download</Button>
                <Button variant="outlined" onClick={() => handleView(file)}>View</Button>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
};

export default App;
