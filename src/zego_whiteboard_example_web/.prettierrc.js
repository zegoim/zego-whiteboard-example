module.exports = {
    semi: true,
    trailingComma: 'all',
    singleQuote: true,
    printWidth: 120,
    tabWidth: 4,
    trailingComma: 'none',
    arrowParens: 'always',
    overrides: [
        {
            files: ['*.html', '*.css', '*.json'],
            options: {
                tabWidth: 2
            }
        }
    ]
};
