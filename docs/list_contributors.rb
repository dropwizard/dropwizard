#!/usr/bin/env ruby
require 'octokit'

Octokit.configure do |c|
  # Provide an Access Token to prevent running into the hourly rate-limit
  # see https://help.github.com/articles/creating-an-access-token-for-command-line-use
  #c.access_token = ''
  c.auto_paginate = true
end

contributors = Octokit.contributors('dropwizard/dropwizard')
contributors.each do |c|
  user = Octokit.user(c.login)
  name = if user.name.nil? then user.login else user.name end
  puts "* `#{name} <#{user.html_url}>`_"
end
