import { createSlice } from '@reduxjs/toolkit';

const filesSlice = createSlice({
  name: 'files',
  initialState: {
    items: [],
  },
  reducers: {
    setFiles: (state, action) => {
      state.items = action.payload;
    },
  },
});

export const { setFiles } = filesSlice.actions;
export default filesSlice.reducer;