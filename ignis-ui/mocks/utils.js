var exports = module.exports = {}

exports.json402 = {
  errors: [{
    errorCode: '402',
    errorMessage: 'Validation Failed'
  }]
}

exports.json403 = {
  errors: [{
    errorCode: '403',
    errorMessage: 'Forbidden'
  }]
}

exports.json404 = {
  errors: [{
    errorCode: '404',
    errorMessage: 'Not Found'
  }]
}

exports.randomString = function(num) {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890'
  let str = ''

  for ( let i = 0; i < num; i++ ) {
    str += chars[Math.floor(Math.random() * 36)]
  }

  return str
}

exports.randomNumber = function(min, max) {
  if (min < 0) {
      return Math.floor(min + Math.random() * (Math.abs(min) + max));
  }
  else {
      return Math.floor(min + Math.random() * max);
  }
}

exports.randomBoolean = function() {
  return Math.random() >= 0.5;
}

exports.generateDocument = function(num){
  const arr = []

  for ( let i = 0; i < num; i++ ) {
    arr.push({
      "name" : "Document " + this.randomString(5),
      "url" : "/api/documents/" + this.randomString(5)
    })
  }

  return arr
}

exports.randomSentence = function() {
  const verbs =
  [
      ["change to", "changed to", "edited to", "amend to", "edit to"],
      ["look at", "looks at", "looking at", "looked at", "looked at"],
      ["choose", "chooses", "choosing", "chose", "chosen"]
  ];
  const tenses =
  [
      {name:"Present", singular:1, plural:0, format:"%subject %verb %complement"},
      {name:"Past", singular:3, plural:3, format:"%subject %verb %complement"},
      {name:"Present Continues", singular:2, plural:2, format:"%subject %be %verb %complement"}
  ];
  const subjects =
  [
      {name:"I", be:"am", singular:0},
      {name:"You", be:"are", singular:0},
      {name:"There", be:"is", singular:1}
  ];
  const complementsForVerbs =
  [
      ["1,000,000", "better", "incorrect", "lower"],
      ["for a change", "them", "the figure", "the cell"],
      ["a different cell to amend", "to ignore that figure"]
  ]
  Array.prototype.random = function(){return this[Math.floor(Math.random() * this.length)];};

  let index = Math.floor(verbs.length * Math.random());
  let tense = tenses.random();
  let subject = subjects.random();
  let verb = verbs[index];
  let complement = complementsForVerbs[index];
  let str = tense.format;
  str = str.replace("%subject", subject.name).replace("%be", subject.be);
  str = str.replace("%verb", verb[subject.singular ? tense.singular : tense.plural]);
  str = str.replace("%complement", complement.random());
  return str;

}
