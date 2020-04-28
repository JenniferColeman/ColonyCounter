from flask import Flask
from flask_cors import CORS


app = Flask(__name__)
CORS(app)

@app.route('/api/new')
def newImg():
    return 'Hello new'


@app.route('/api/edit')
def editImg():
    return 'Hello edit'


@app.route('/api/del')
def delImg():
    return 'deleto'
