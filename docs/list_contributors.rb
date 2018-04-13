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

contributors = []
repo_contributors = Octokit.contributors('dropwizard/dropwizard')
repo_contributors.each do |c|
  user = Octokit.user(c.login)
  contributor = {}
  contributor['login'] = user.login
  if user.name.nil? then
    contributor['name'] = user.login
  else
    contributor['name'] = user.name
  end
  contributors << contributor
end

sorted_contributors = contributors.sort_by{ |contributor| contributor['name'].downcase }
sorted_contributors.each do |c|
  puts "* :ghuser:`#{c['name']} <#{c['login']}>`"
end
