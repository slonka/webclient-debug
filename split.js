const fs = require('fs');
const path = require('path');
const {promisify} = require('util');

const readFile = promisify(fs.readFile);
const writeFile = promisify(fs.writeFile);

const file = process.argv[2];
const i = parseInt(process.argv[3], 10) || 1;

async function splitFile() {
    const contents = (await readFile(path.join('.', file))).toString();

    const splits = contents.split(/\<\/easy_to_find_method_name_123\>|\<easy_to_find_method_name_123\>/);
    await writeFile(path.join('.', 'output', `first_call_${i}.txt`), splits[1]);
    await writeFile(path.join('.', 'output', `second_call_${i}.txt`), splits[3]);
}

splitFile();
