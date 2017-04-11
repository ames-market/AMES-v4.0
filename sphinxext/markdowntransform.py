import os
import pypandoc


def convert(app):
    root = './'
    for f in ['README.md']:
        pypandoc.convert_file(os.path.join(root, '..', f), 'rst', outputfile=os.path.join(root, '..', f).replace('.md', '.rst'))
    for root, _, files in os.walk('./'):
        for f in files:
            if f.endswith('.md'):
                pypandoc.convert_file(os.path.join(root, f), 'rst', outputfile=os.path.join(root, f).replace('.md', '.rst'))


def setup(app):
    app.connect('builder-inited', convert)
