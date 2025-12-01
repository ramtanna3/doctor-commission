import React from 'react';
import MenuItem from '@mui/material/MenuItem';
import Select from '@mui/material/Select';
import type { SelectChangeEvent } from '@mui/material/Select';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';

export interface DropdownOption {
  value: string | number;
  label: string;
}

export interface DropdownProps {
  label: string;
  name: string;
  value: string | number | undefined;
  options: DropdownOption[];
  onChange: (event: SelectChangeEvent<string | number>) => void;
  required?: boolean;
  size?: 'small' | 'medium';
  sx?: object;
}

const Dropdown: React.FC<DropdownProps> = ({
  label,
  name,
  value,
  options,
  onChange,
  required = false,
  size = 'small',
  sx = {},
}) => (
  <FormControl size={size} sx={sx} required={required}>
    <InputLabel id={`${name}-label`}>{label}</InputLabel>
    <Select
      labelId={`${name}-label`}
      name={name}
      value={value ?? ''}
      label={label}
      onChange={onChange}
      required={required}
    >
      <MenuItem value=""><em>Select {label}</em></MenuItem>
      {options.map(opt => (
        <MenuItem key={opt.value} value={opt.value}>{opt.label}</MenuItem>
      ))}
    </Select>
  </FormControl>
);

export default Dropdown;
