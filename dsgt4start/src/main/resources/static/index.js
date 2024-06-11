import { setupAuth, wireUpAuthChange } from './auth.js';
import { wireUpEvents } from './ui.js';

setupAuth();
wireUpAuthChange();
wireUpEvents();

