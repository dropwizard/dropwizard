#!/usr/bin/env ruby
require 'octokit'

# Provide an Access Token in the OCTOKIT_ACCESS_TOKEN environemnt variable
# to prevent running into the hourly rate-limit.
#
# See also:
#   * https://octokit.github.io/octokit.rb/#Authentication and
#   * https://help.github.com/articles/creating-a-personal-access-token-for-the-command-line/
#
# Example:
#   OCTOKIT_ACCESS_TOKEN=11223344556677889900cafebabedeadbeef0000 ruby list_contributors.rb

Octokit.configure do |c|
  c.auto_paginate = true
end

contributors = Octokit.contributors('dropwizard/dropwizard')
contributors.each do |c|
  user = Octokit.user(c.login)
  name = if user.name.nil? then user.login else user.name end
  puts "* `#{name} <#{user.html_url}>`_"
end
