export default function FormField({ id, label, error, as: Tag = 'input', children, ...props }) {
    return (
        <div>
            <label htmlFor={id} className="label">
                {label}
            </label>
            <Tag
                id={id}
                name={id}
                className={`input ${error ? 'input-error' : ''}`}
                aria-invalid={Boolean(error)}
                aria-describedby={error ? `${id}-error` : undefined}
                {...props}
            >
                {children}
            </Tag>
            {error && (
                <p id={`${id}-error`} className="field-error">
                    {error}
                </p>
            )}
        </div>
    );
}