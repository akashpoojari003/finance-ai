const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;
const PHONE_RE = /^\+?[0-9]{10,15}$/;

export const GENDERS = [
    { value: 'MALE', label: 'Male' },
    { value: 'FEMALE', label: 'Female' },
    { value: 'OTHER', label: 'Other' },
];

export function normalizePhone(phone) {
    return (phone || '').replace(/[\s-]/g, '');
}

export function validateLogin({ email, password }) {
    const errors = {};
    if (!email.trim()) errors.email = 'Email is required';
    else if (!EMAIL_RE.test(email.trim())) errors.email = 'Enter a valid email address';

    if (!password) errors.password = 'Password is required';
    return errors;
}

export function validateSignup({ name, email, password, gender, phone }) {
    const errors = {};

    if (!name.trim()) errors.name = 'Name is required';
    else if (name.trim().length > 100) errors.name = 'Name must be 100 characters or fewer';

    if (!email.trim()) errors.email = 'Email is required';
    else if (!EMAIL_RE.test(email.trim())) errors.email = 'Enter a valid email address';
    else if (email.trim().length > 150) errors.email = 'Email must be 150 characters or fewer';

    if (!password) errors.password = 'Password is required';
    else if (password.length < 8) errors.password = 'Password must be at least 8 characters';
    else if (password.length > 72) errors.password = 'Password must be 72 characters or fewer';

    if (!GENDERS.some((g) => g.value === gender)) errors.gender = 'Select a gender';

    const cleanPhone = normalizePhone(phone);
    if (cleanPhone && !PHONE_RE.test(cleanPhone)) {
        errors.phone = 'Use 10-15 digits, optional leading +';
    }

    return errors;
}