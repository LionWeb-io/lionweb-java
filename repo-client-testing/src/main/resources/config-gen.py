import os

code = f"""{{
  "server": {{
    "serverPort": 3005,
    "expectedToken": null
  }},
  "startup": {{
    "createDatabase": "always",
    "createRepositories": [{{
        "name": "default",
        "history": false,
        "create": "if-not-exists",
        "lionWebVersion": "{os.environ['LIONWEB_VERSION']}"
      }}
    ]
  }},
  "logging": {{
    "request": "silent",
    "database": "silent",
    "express": "silent"
  }},
  "postgres": {{
    "database": {{
      "host": "{os.environ['PGHOST']}",
      "user": "{os.environ['PGUSER']}",
      "db": "{os.environ['PGDB']}",
      "password": "{os.environ['PGPASSWORD']}",
      "port": "{os.environ['PGPORT']}"
    }},
    "certificates": {{
      "rootcert": null,
      "rootcertcontent": null
    }}
  }}
}}
"""
with open("server-config.json", "w") as file:
    file.write(code)
