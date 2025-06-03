import React from 'react';
import '../index.css';
import { Provider } from 'react-redux';
import store from '../store';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { CacheProvider } from '@emotion/react';
import createCache from '@emotion/cache';

// Create a cache for emotion
const cache = createCache({
  key: 'css',
  prepend: true,
});

// Create a theme instance
const theme = createTheme();

function MyApp({ Component, pageProps }) {
  return (
    <Provider store={store}>
      <CacheProvider value={cache}>
        <ThemeProvider theme={theme}>
          <CssBaseline />
          <Component {...pageProps} />
        </ThemeProvider>
      </CacheProvider>
    </Provider>
  );
}

export default MyApp;
