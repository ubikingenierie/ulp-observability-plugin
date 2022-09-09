class InvalidLineError extends Error {
    constructor(message: string) {
        super('Encountered invalid line: ' + message);
    }
}

Object.defineProperty(InvalidLineError.prototype, 'name', {
    value: InvalidLineError.name
});

export default InvalidLineError;
