var glob = require('glob');
const fs = require('fs');
const path = require('path');
const {
    promisify
} = require('util');

const readFile = promisify(fs.readFile);

function standardDeviation(values) {
    let avg = average(values);

    let squareDiffs = values.map(function (value) {
        var diff = value - avg;
        var sqrDiff = diff * diff;
        return sqrDiff;
    });

    let avgSquareDiff = average(squareDiffs);

    let stdDev = Math.sqrt(avgSquareDiff);
    return stdDev;
}

function average(data) {
    let sum = data.reduce(function (sum, value) {
        return sum + value;
    }, 0);

    let avg = sum / data.length;
    return avg;
}

const file = process.argv[2];

// options is optional
glob(`./output/${file}_*.txt`, {}, async function (er, files) {
    let times = [];

    console.log('Mean:');

    for (let file of files) {
        const contents = (await readFile(file)).toString();
        contents.split('\n').forEach((line, i) => {
            const val = parseFloat(line);
            if (times[i] === undefined) {
                times[i] = [val]
            } else {
                times[i].push(val);
            }
        });
    }

    for (let time of times) {
        let sum = time.reduce((previous, current) => current += previous);
        let avg = sum / time.length;
        console.log(avg);
    }

    console.log('---');
    console.log('Standard deviation:');

    for (let time of times) {
        console.log(standardDeviation(time));
    }
});