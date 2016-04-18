var Http = require('http'),
    Url = require('url'),
    datastore = {
      webapp_terminating: [],
      webapp_originating: {
        waiting: [],
        passed_to_gateway: [],
      },
      delivery_reports: [],
      errors: [],
    },
    handlers = {
      '/': function(req, res) {
        var response;
        switch(req.method) {
          case 'GET':
            return res.end(ok({ datastore: datastore }));
          case 'POST':
            return readBody(req)
              .then(JSON.parse)
              .then(function(message) {
                datastore.webapp_originating.waiting.push(message);
                res.end(ok({ added_message: message }));
              });
          default: throw new Error('Unhandled method.');
        }
      },
      '/error': function(req, res) {
        if(req.method !== 'POST') throw new Error('Unhandled method.');
        return readBody(req)
          .then(JSON.parse)
          .then(function(requestedError) {
            datastore.errors.push(requestedError);
            res.end(ok({ added_error: requestedError }));
          });
      },
      '/app': function(req, res) {
        // enforce expected headers
        assertHeader(req, 'Accept', 'application/json');
        assertHeader(req, 'Accept-Charset', 'utf-8');
        assertHeader(req, 'Accept-Encoding', 'gzip');
        assertHeader(req, 'Cache-Control', 'no-cache');
        assertHeader(req, 'Content-Type', 'application/json');

        if(datastore.errors.length) {
          throw new Error(datastore.errors.shift());
        }

        return readBody(req)
          .then(JSON.parse)
          .then(function(json) {
            push(datastore.webapp_terminating, json.messages);
            push(datastore.delivery_reports, json.deliveries);

            res.end(JSON.stringify({
              messages: datastore.webapp_originating.waiting,
            }));
            push(datastore.webapp_originating.passed_to_gateway,
                datastore.webapp_originating.waiting);
            datastore.webapp_originating.waiting.length = 0;
          });
      },
    };

function ok(r) {
  if(!r) r = {};
  r.ok = true;
  return JSON.stringify(r, null, 2);
}

function readBody(req) {
  var body = '';
  return new Promise(function(resolve, reject) {
    req.on('data', function(data) {
      body += data.toString();
    });
    req.on('end', function() {
      resolve(body);
    });
    req.on('error', reject);
  });
}

function push(arr, vals) {
  arr.push.apply(arr, vals);
}

function assertHeader(req, key, expected) {
  var actual = req.headers[key.toLowerCase()];
  if(actual !== expected)
    throw new Error(
        'Bad value for header "' + key + '": ' +
        'expected "' + expected + '", ' +
        'but got "' + actual + '"');
}

Http.createServer(function(req, res) {
  var url = Url.parse(req.url),
      handler = handlers[url.pathname],
      requestBody = req.read();

  console.log(new Date(), req.method, req.url);

  function error(message) {
    var i, body = {
      err: message,
      method: req.method,
      url: url,
    };
    if(arguments.length > 1) {
      body.extras = Array.prototype.slice.call(arguments, 1);
    }
    res.writeHead(500);
    res.end(JSON.stringify(body, null, 2));
  }

  if(!handler) {
    return error('Path not found for URL: ');
  }

  Promise.resolve()
    .then(function() {
      return handler(req, res);
    })
    .catch(function(e) {
      error(e.message);
    });
}).listen(process.env.PORT || 8000);
